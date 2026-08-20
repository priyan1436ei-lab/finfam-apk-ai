/**
 * FinFam Real Payment Gateway Backend Server
 * Architecture: Express.js + Razorpay SDK + Firebase Admin (Firestore)
 * Supports: UPI, UPI Intent, UPI QR, Cards, Net Banking, Wallets, Webhooks, Signature Verification & Refunds
 */

const express = require('express');
const cors = require('cors');
const crypto = require('crypto');
const Razorpay = require('razorpay');
const admin = require('firebase-admin');

const app = express();
app.use(cors());

// Raw body parser for Razorpay webhook signature verification
app.use('/api/payment/webhook', express.raw({ type: 'application/json' }));
app.use(express.json());

// Initialize Firebase Admin SDK
if (!admin.apps.length) {
    try {
        admin.initializeApp({
            credential: admin.credential.applicationDefault()
        });
    } catch (e) {
        console.warn('Firebase Admin initialized in local environment mode');
    }
}
const db = admin.firestore ? admin.firestore() : null;

// Razorpay Instance (Credentials read securely from environment variables)
const RAZORPAY_KEY_ID = process.env.RAZORPAY_KEY_ID || 'rzp_test_FinFamElite2026';
const RAZORPAY_KEY_SECRET = process.env.RAZORPAY_KEY_SECRET || 'FinFamSecretKey2026';
const RAZORPAY_WEBHOOK_SECRET = process.env.RAZORPAY_WEBHOOK_SECRET || 'FinFamWebhookSecret2026';

const razorpay = new Razorpay({
    key_id: RAZORPAY_KEY_ID,
    key_secret: RAZORPAY_KEY_SECRET
});

// Authoritative Server-Side Plan Price Registry
const PLAN_REGISTRY = {
    'premium_monthly': {
        title: 'FinFam Premium Monthly',
        amountInr: 199.0,
        amountPaise: 19900,
        durationDays: 30
    },
    'premium_annual': {
        title: 'FinFam Premium Annual',
        amountInr: 1499.0,
        amountPaise: 149900,
        durationDays: 365
    },
    'premium_lifetime': {
        title: 'FinFam Lifetime Founder Shield',
        amountInr: 3999.0,
        amountPaise: 399900,
        durationDays: 36500
    }
};

/**
 * 1. POST /api/payment/create-order
 * Validates plan, computes price server-side, generates Razorpay order, stores in Firestore.
 */
app.post('/api/payment/create-order', async (req, res) => {
    try {
        const { planId, userId = 'user_priyanshu_sharma' } = req.body;

        const plan = PLAN_REGISTRY[planId];
        if (!plan) {
            return res.status(400).json({ success: false, error: 'Invalid or unknown subscription plan ID' });
        }

        // Create Real Order in Razorpay Gateway
        const orderOptions = {
            amount: plan.amountPaise,
            currency: 'INR',
            receipt: `rcpt_${Date.now()}`,
            notes: {
                userId: userId,
                planId: planId,
                planTitle: plan.title,
                source: 'FinFam Android App'
            }
        };

        const razorpayOrder = await razorpay.orders.create(orderOptions);

        // Store Order in Firestore
        if (db) {
            await db.collection('orders').doc(razorpayOrder.id).set({
                orderId: razorpayOrder.id,
                userId: userId,
                planId: planId,
                amount: plan.amountInr,
                currency: 'INR',
                status: 'CREATED',
                createdAt: admin.firestore.FieldValue.serverTimestamp()
            });
        }

        return res.status(200).json({
            success: true,
            orderId: razorpayOrder.id,
            amountPaise: plan.amountPaise,
            currency: 'INR',
            keyId: RAZORPAY_KEY_ID,
            planId: planId
        });
    } catch (error) {
        console.error('Error creating Razorpay order:', error);
        return res.status(500).json({ success: false, error: error.message || 'Failed to create Razorpay order' });
    }
});

/**
 * 2. POST /api/payment/verify
 * Cryptographically verifies Razorpay signature using HMAC-SHA256 and activates Premium.
 */
app.post('/api/payment/verify', async (req, res) => {
    try {
        const {
            razorpayPaymentId,
            razorpayOrderId,
            razorpaySignature,
            planId,
            paymentMethod = 'UPI',
            userId = 'user_priyanshu_sharma'
        } = req.body;

        if (!razorpayPaymentId || !razorpayOrderId || !razorpaySignature) {
            return res.status(400).json({ success: false, error: 'Missing payment signature verification parameters' });
        }

        // Cryptographic Signature Verification: HMAC_SHA256(order_id + "|" + payment_id, secret)
        const hmac = crypto.createHmac('sha256', RAZORPAY_KEY_SECRET);
        hmac.update(`${razorpayOrderId}|${razorpayPaymentId}`);
        const generatedSignature = hmac.digest('hex');

        const isSignatureValid = (generatedSignature === razorpaySignature);

        if (!isSignatureValid) {
            if (db) {
                await db.collection('payments').doc(razorpayPaymentId).set({
                    paymentId: razorpayPaymentId,
                    orderId: razorpayOrderId,
                    userId: userId,
                    status: 'FAILED',
                    failureReason: 'HMAC Signature Mismatch',
                    updatedAt: admin.firestore.FieldValue.serverTimestamp()
                }, { merge: true });
            }
            return res.status(400).json({
                success: false,
                status: 'FAILED',
                error: 'Payment signature verification failed. Premium remains inactive.'
            });
        }

        // Calculate subscription validity
        const plan = PLAN_REGISTRY[planId] || PLAN_REGISTRY['premium_annual'];
        const startDate = new Date();
        const endDate = new Date(startDate.getTime() + (plan.durationDays * 24 * 60 * 60 * 1000));
        const validUntil = endDate.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });

        if (db) {
            const batch = db.batch();

            // 1. Save Transaction to Firestore `payments/{paymentId}`
            const paymentRef = db.collection('payments').doc(razorpayPaymentId);
            batch.set(paymentRef, {
                paymentId: razorpayPaymentId,
                orderId: razorpayOrderId,
                userId: userId,
                planId: planId,
                amount: plan.amountInr,
                currency: 'INR',
                status: 'SUCCESS',
                paymentMethod: paymentMethod,
                createdAt: admin.firestore.FieldValue.serverTimestamp(),
                paidAt: admin.firestore.FieldValue.serverTimestamp(),
                refundStatus: null
            });

            // 2. Activate Subscription in Firestore `subscriptions/{userId}`
            const subRef = db.collection('subscriptions').doc(userId);
            batch.set(subRef, {
                userId: userId,
                planId: planId,
                status: 'ACTIVE',
                startDate: startDate.toISOString(),
                endDate: endDate.toISOString(),
                validUntil: validUntil,
                latestPaymentId: razorpayPaymentId,
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            });

            await batch.commit();
        }

        return res.status(200).json({
            success: true,
            status: 'SUCCESS',
            paymentId: razorpayPaymentId,
            orderId: razorpayOrderId,
            validUntil: validUntil,
            message: 'Payment verified and FinFam Premium activated.'
        });
    } catch (error) {
        console.error('Error verifying payment:', error);
        return res.status(500).json({ success: false, error: error.message || 'Payment verification server error' });
    }
});

/**
 * 3. POST /api/payment/refund
 * Initiates real refund through Razorpay and updates Firestore.
 */
app.post('/api/payment/refund', async (req, res) => {
    try {
        const { paymentId, orderId, reason = 'Customer Cancellation', userId } = req.body;

        if (!paymentId) {
            return res.status(400).json({ success: false, error: 'Payment ID is required for refund' });
        }

        // Call Razorpay API to execute real refund
        const refundResponse = await razorpay.payments.refund(paymentId, {
            notes: {
                reason: reason,
                orderId: orderId,
                requestedBy: userId || 'user'
            }
        });

        // Update Firestore
        if (db) {
            await db.collection('payments').doc(paymentId).update({
                status: 'REFUNDED',
                refundStatus: 'REFUNDED',
                refundId: refundResponse.id,
                refundedAt: admin.firestore.FieldValue.serverTimestamp()
            });
        }

        return res.status(200).json({
            success: true,
            refundId: refundResponse.id,
            status: 'REFUNDED',
            amount: refundResponse.amount / 100,
            message: `Refund of ₹${refundResponse.amount / 100} initiated successfully.`
        });
    } catch (error) {
        console.error('Error processing refund:', error);
        return res.status(500).json({ success: false, error: error.message || 'Refund processing failed' });
    }
});

/**
 * 4. POST /api/payment/webhook
 * Idempotent Razorpay Webhook Handler for automated payment & refund events.
 */
app.post('/api/payment/webhook', async (req, res) => {
    try {
        const signature = req.headers['x-razorpay-signature'];
        const bodyBuffer = req.body;

        // Verify Webhook Signature
        const expectedSignature = crypto
            .createHmac('sha256', RAZORPAY_WEBHOOK_SECRET)
            .update(bodyBuffer)
            .digest('hex');

        if (expectedSignature !== signature) {
            console.error('Webhook signature mismatch');
            return res.status(400).send('Invalid Webhook Signature');
        }

        const event = JSON.parse(bodyBuffer.toString());
        const eventId = req.headers['x-razorpay-event-id'] || event.event;

        // Idempotency check in Firestore
        if (db) {
            const eventRef = db.collection('webhook_events').doc(eventId);
            const doc = await eventRef.get();
            if (doc.exists) {
                return res.status(200).send('Event already processed');
            }
            await eventRef.set({ processedAt: admin.firestore.FieldValue.serverTimestamp(), event: event.event });
        }

        // Handle specific Razorpay events
        if (event.event === 'payment.captured') {
            const payment = event.payload.payment.entity;
            console.log(`Payment captured: ${payment.id} for amount ₹${payment.amount / 100}`);
        } else if (event.event === 'refund.processed') {
            const refund = event.payload.refund.entity;
            console.log(`Refund processed: ${refund.id} for payment ${refund.payment_id}`);
        }

        return res.status(200).json({ status: 'ok' });
    } catch (error) {
        console.error('Webhook processing error:', error);
        return res.status(500).send('Webhook Server Error');
    }
});

/**
 * 5. GET /api/payment/status/:orderId
 */
app.get('/api/payment/status/:orderId', async (req, res) => {
    try {
        const { orderId } = req.params;
        if (db) {
            const orderDoc = await db.collection('orders').doc(orderId).get();
            if (orderDoc.exists) {
                return res.status(200).json(orderDoc.data());
            }
        }
        return res.status(200).json({ orderId, status: 'PENDING' });
    } catch (error) {
        return res.status(500).json({ error: error.message });
    }
});

const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
    console.log(`FinFam Real Payment Gateway Server running on port ${PORT}`);
});
